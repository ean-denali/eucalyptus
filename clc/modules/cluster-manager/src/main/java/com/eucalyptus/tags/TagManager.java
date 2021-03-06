/*************************************************************************
 * Copyright 2009-2012 Eucalyptus Systems, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Please contact Eucalyptus Systems, Inc., 6755 Hollister Ave., Goleta
 * CA 93117, USA or visit http://www.eucalyptus.com/licenses/ if you need
 * additional information or have any questions.
 ************************************************************************/
package com.eucalyptus.tags;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;
import com.eucalyptus.auth.Permissions;
import com.eucalyptus.auth.policy.PolicySpec;
import com.eucalyptus.cloud.CloudMetadata;
import com.eucalyptus.cloud.util.MetadataException;
import com.eucalyptus.cloud.util.NoSuchMetadataException;
import com.eucalyptus.configurable.ConfigurableClass;
import com.eucalyptus.configurable.ConfigurableField;
import com.eucalyptus.context.Context;
import com.eucalyptus.context.Contexts;
import com.eucalyptus.entities.Entities;
import com.eucalyptus.entities.TransactionException;
import com.eucalyptus.images.ImageInfo;
import com.eucalyptus.images.Images;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.Exceptions;
import com.eucalyptus.util.OwnerFullName;
import com.eucalyptus.util.RestrictedTypes;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import edu.ucsb.eucalyptus.cloud.InvalidParameterValueException;
import edu.ucsb.eucalyptus.msgs.CreateTagsResponseType;
import edu.ucsb.eucalyptus.msgs.CreateTagsType;
import edu.ucsb.eucalyptus.msgs.DeleteResourceTag;
import edu.ucsb.eucalyptus.msgs.DeleteTagsResponseType;
import edu.ucsb.eucalyptus.msgs.DeleteTagsType;
import edu.ucsb.eucalyptus.msgs.DescribeTagsResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeTagsType;
import edu.ucsb.eucalyptus.msgs.ResourceTag;
import edu.ucsb.eucalyptus.msgs.TagInfo;

/**
 * Service implementation for Tag operations
 */
@ConfigurableClass( root = "tagging", description = "Parameters controlling tagging")
public class TagManager {
  private static final Logger log = Logger.getLogger( TagManager.class );
  
  private static final Set<String> reservedPrefixes = 
      ImmutableSet.<String>builder().add("aws:").add("euca:").build();
  
  @ConfigurableField(initial = "10", description = "The maximum number of tags per resource for each account")
  public static long MAX_TAGS_PER_RESOURCE = 10;

  public CreateTagsResponseType createTags( final CreateTagsType request ) throws EucalyptusCloudException {
    final CreateTagsResponseType reply = request.getReply( );
    reply.set_return( false );

    final Context context = Contexts.lookup();
    final OwnerFullName ownerFullName = context.getUserFullName();
    final List<String> resourceIds = Objects.firstNonNull( request.getResourcesSet(), Collections.<String>emptyList() );
    final List<ResourceTag> resourceTags = Objects.firstNonNull( request.getTagSet(), Collections.<ResourceTag>emptyList() );

    for ( final ResourceTag resourceTag : resourceTags ) {
      final String key = resourceTag.getKey();
      final String value = Strings.nullToEmpty( resourceTag.getValue() ).trim();

      if ( Strings.isNullOrEmpty( key ) || key.trim().length() > 128 || isReserved( key ) ) {
        throw new InvalidParameterValueException( "Invalid key (max length 128, must not be empty, reserved prefixes "+reservedPrefixes+"): "+key );     
      }
      if ( value.length() > 256 || isReserved( key ) ) {
        throw new InvalidParameterValueException( "Invalid value (max length 256, reserved prefixes "+reservedPrefixes+"): "+value );
      }
    }
    
    if ( resourceTags.size() > 0 && resourceIds.size() > 0 ) {      
      final Predicate<Void> creator = new Predicate<Void>(){
        @Override
        public boolean apply( final Void v ) {
          final List<CloudMetadata> resources = Lists.transform( resourceIds, resourceLookup() );
          if ( !Iterables.all( resources, Predicates.and( Predicates.notNull(), typeSpecificFilters(), RestrictedTypes.filterPrivileged() ) )  ) {
            return false;            
          }

          //TODO:STEVE: quotas for tags?
          for ( final CloudMetadata resource : resources ) {
            final Tag example = TagSupport.fromResource( resource ).example( resource, ownerFullName, null, null );
            if ( Entities.count( example ) + resourceTags.size() > MAX_TAGS_PER_RESOURCE ) {
              return false;              
            }
          }

          for ( final CloudMetadata resource : resources ) {
            for ( final ResourceTag resourceTag : resourceTags ) {
              final String key = Strings.nullToEmpty( resourceTag.getKey() ).trim();
              final String value = Strings.nullToEmpty( resourceTag.getValue() ).trim();
              TagSupport.fromResource( resource ).createOrUpdate( resource, ownerFullName, key, value );
            }
          }

          return true;
        }
      };
      
      reply.set_return( Entities.asTransaction( Tag.class, creator ).apply( null ) );
    }
    
    return reply;
  }

  public DeleteTagsResponseType deleteTags( final DeleteTagsType request ) {
    final DeleteTagsResponseType reply = request.getReply( );
    reply.set_return( false );

    final Context context = Contexts.lookup();
    final OwnerFullName ownerFullName = context.getUserFullName().asAccountFullName();
    final List<String> resourceIds = Objects.firstNonNull( request.getResourcesSet(), Collections.<String>emptyList() );
    final List<DeleteResourceTag> resourceTags = Objects.firstNonNull( request.getTagSet(), Collections.<DeleteResourceTag>emptyList() );

    if ( resourceIds.size() > 0 && resourceIds.size() > 0 ) {
      final Predicate<Void> delete = new Predicate<Void>(){
        @Override
        public boolean apply( final Void v ) {
          final Iterable<CloudMetadata> resources = Iterables.filter( Iterables.transform( resourceIds, resourceLookup() ), Predicates.notNull() );
          for ( final CloudMetadata resource : resources ) {
            for ( final DeleteResourceTag resourceTag : resourceTags ) {
              try {
                final Tag example = TagSupport.fromResource( resource ).example( resource, ownerFullName, resourceTag.getKey(), resourceTag.getValue() );                
                if ( example != null && Permissions.isAuthorized( 
                    PolicySpec.VENDOR_EC2,
                    PolicySpec.EC2_RESOURCE_TAG,
                    example.getResourceType() + ":" + example.getResourceId() + ":" + example.getKey(),
                    context.getAccount(),
                    PolicySpec.EC2_DELETETAGS,
                    context.getUser() ) ) {
                  Tags.delete( example );
                }
              } catch ( NoSuchMetadataException e ) {
                log.debug( e, e );
              } catch ( MetadataException e ) {
                throw Exceptions.toUndeclared(e);
              } 
            }
          }

          return true;
        }
      };

      reply.set_return( Entities.asTransaction( Tag.class, delete ).apply( null ) );
    }

    return reply;
  }

  public DescribeTagsResponseType describeTags( final DescribeTagsType request ) throws Exception {
    final DescribeTagsResponseType reply = request.getReply( );
    final Context context = Contexts.lookup();
    
    //TODO:STEVE: Cloud admin can list all tags?
    final Filter filter = Filters.generate( request.getFilterSet(), Tag.class );
    final Ordering<Tag> ordering = Ordering.natural().onResultOf( Tags.resourceId() )
        .compound( Ordering.natural().onResultOf( Tags.key() ) )
        .compound( Ordering.natural().onResultOf( Tags.value() ) );
    for ( final Tag tag : ordering.sortedCopy( Tags.list(
        context.getUserFullName().asAccountFullName(),
        filter.asPredicate(),
        filter.asCriterion(),
        filter.getAliases() ) ) ) {
      if ( Permissions.isAuthorized( 
          PolicySpec.VENDOR_EC2,
          tag.getResourceType(),
          tag.getKey(),
          context.getAccount(), //TODO:STEVE: this is wrong, should be the account of the resource, not caller.
          PolicySpec.describeAction( PolicySpec.VENDOR_EC2, tag.getResourceType() ),
          context.getUser() ) ) { //TODO:STEVE: this permission check is not sufficient (e.g. launch permissions, create volume permissions)
        final TagInfo info = new TagInfo();
        info.setKey( tag.getKey() );
        info.setValue( tag.getValue() );
        info.setResourceId( tag.getResourceId() );
        info.setResourceType( tag.getResourceType() );
        reply.getTagSet().add( info );
      }      
    }
    
    return reply;
  }

  private static boolean isReserved( final String text ) {
    return Iterables.any( reservedPrefixes, prefix( text ) );          
  }
  
  private static Predicate<String> prefix( final String text ) {
    return new Predicate<String>() {
      @Override
      public boolean apply( final String prefix ) {
        return text != null && text.trim().startsWith( prefix );
      }
    };  
  } 

  private static Predicate<Object> typeSpecificFilters() {
    return TypeSpecificFilters.INSTANCE;
  }

  /**
   * A function to lookup cloud metadata by resource identifier.
   * 
   * The returned function may return null values. 
   */
  private static Function<String, CloudMetadata> resourceLookup() {
    return new Function<String,CloudMetadata>() {
      @Override
      public CloudMetadata apply( final String resourceId ) {
        try {
          final TagSupport tagSupport = TagSupport.fromIdentifier( resourceId );
          if ( tagSupport != null ) {
            return tagSupport.lookup( resourceId );
          }
        } catch ( TransactionException e ) {
          throw Exceptions.toUndeclared( e );
        } catch ( NoSuchElementException e ) {
          // same as invalid identifier format
        }
        return null;
      }
    };
  }

  /**
   * Access filtering using type specific approach (e.g. launch permissions for images)
   */
  private static enum TypeSpecificFilters implements Predicate<Object> {
    INSTANCE;

    private List<Predicate<Object>> predicates = ImmutableList.of(
        typedPredicate( Images.FilterPermissions.INSTANCE, ImageInfo.class )
    );

    private <PT> Predicate<Object> typedPredicate( final Predicate<? super PT> predicate,
                                                   final Class<PT> predicateTarget ) {
      return new Predicate<Object>() {
        @Override
        public boolean apply( @Nullable final Object object ) {
          return
              !predicateTarget.isInstance( object ) ||
              predicate.apply( predicateTarget.cast( object ) );
        }
      };
    }

    @Override
    public boolean apply( final Object object ) {
      return Predicates.and( predicates ).apply( object );
    }
  }
}
