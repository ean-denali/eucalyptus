/*************************************************************************
 * Copyright 2009-2013 Eucalyptus Systems, Inc.
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
package com.eucalyptus.autoscaling;

import static com.eucalyptus.autoscaling.common.AutoScalingResourceName.InvalidResourceNameException;
import static com.eucalyptus.autoscaling.common.AutoScalingMetadata.AutoScalingGroupMetadata;
import static com.eucalyptus.autoscaling.common.AutoScalingMetadata.LaunchConfigurationMetadata;
import static com.eucalyptus.autoscaling.common.AutoScalingResourceName.Type.autoScalingGroup;
import java.util.EnumSet;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import com.eucalyptus.auth.AuthQuotaException;
import com.eucalyptus.auth.principal.AccountFullName;
import com.eucalyptus.autoscaling.common.AutoScalingGroupType;
import com.eucalyptus.autoscaling.common.AutoScalingMetadatas;
import com.eucalyptus.autoscaling.common.AutoScalingResourceName;
import com.eucalyptus.autoscaling.common.BlockDeviceMappingType;
import com.eucalyptus.autoscaling.common.CreateAutoScalingGroupResponseType;
import com.eucalyptus.autoscaling.common.CreateAutoScalingGroupType;
import com.eucalyptus.autoscaling.common.CreateLaunchConfigurationResponseType;
import com.eucalyptus.autoscaling.common.CreateLaunchConfigurationType;
import com.eucalyptus.autoscaling.common.CreateOrUpdateTagsResponseType;
import com.eucalyptus.autoscaling.common.CreateOrUpdateTagsType;
import com.eucalyptus.autoscaling.common.DeleteAutoScalingGroupResponseType;
import com.eucalyptus.autoscaling.common.DeleteAutoScalingGroupType;
import com.eucalyptus.autoscaling.common.DeleteLaunchConfigurationResponseType;
import com.eucalyptus.autoscaling.common.DeleteLaunchConfigurationType;
import com.eucalyptus.autoscaling.common.DeleteNotificationConfigurationResponseType;
import com.eucalyptus.autoscaling.common.DeleteNotificationConfigurationType;
import com.eucalyptus.autoscaling.common.DeletePolicyResponseType;
import com.eucalyptus.autoscaling.common.DeletePolicyType;
import com.eucalyptus.autoscaling.common.DeleteScheduledActionResponseType;
import com.eucalyptus.autoscaling.common.DeleteScheduledActionType;
import com.eucalyptus.autoscaling.common.DeleteTagsResponseType;
import com.eucalyptus.autoscaling.common.DeleteTagsType;
import com.eucalyptus.autoscaling.common.DescribeAdjustmentTypesResponseType;
import com.eucalyptus.autoscaling.common.DescribeAdjustmentTypesType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingGroupsResponseType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingGroupsType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingInstancesResponseType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingInstancesType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingNotificationTypesResponseType;
import com.eucalyptus.autoscaling.common.DescribeAutoScalingNotificationTypesType;
import com.eucalyptus.autoscaling.common.DescribeLaunchConfigurationsResponseType;
import com.eucalyptus.autoscaling.common.DescribeLaunchConfigurationsType;
import com.eucalyptus.autoscaling.common.DescribeMetricCollectionTypesResponseType;
import com.eucalyptus.autoscaling.common.DescribeMetricCollectionTypesType;
import com.eucalyptus.autoscaling.common.DescribeNotificationConfigurationsResponseType;
import com.eucalyptus.autoscaling.common.DescribeNotificationConfigurationsType;
import com.eucalyptus.autoscaling.common.DescribePoliciesResponseType;
import com.eucalyptus.autoscaling.common.DescribePoliciesType;
import com.eucalyptus.autoscaling.common.DescribeScalingActivitiesResponseType;
import com.eucalyptus.autoscaling.common.DescribeScalingActivitiesType;
import com.eucalyptus.autoscaling.common.DescribeScalingProcessTypesResponseType;
import com.eucalyptus.autoscaling.common.DescribeScalingProcessTypesType;
import com.eucalyptus.autoscaling.common.DescribeScheduledActionsResponseType;
import com.eucalyptus.autoscaling.common.DescribeScheduledActionsType;
import com.eucalyptus.autoscaling.common.DescribeTagsResponseType;
import com.eucalyptus.autoscaling.common.DescribeTagsType;
import com.eucalyptus.autoscaling.common.DescribeTerminationPolicyTypesResponseType;
import com.eucalyptus.autoscaling.common.DescribeTerminationPolicyTypesType;
import com.eucalyptus.autoscaling.common.DisableMetricsCollectionResponseType;
import com.eucalyptus.autoscaling.common.DisableMetricsCollectionType;
import com.eucalyptus.autoscaling.common.EnableMetricsCollectionResponseType;
import com.eucalyptus.autoscaling.common.EnableMetricsCollectionType;
import com.eucalyptus.autoscaling.common.ExecutePolicyResponseType;
import com.eucalyptus.autoscaling.common.ExecutePolicyType;
import com.eucalyptus.autoscaling.common.LaunchConfigurationType;
import com.eucalyptus.autoscaling.common.PutNotificationConfigurationResponseType;
import com.eucalyptus.autoscaling.common.PutNotificationConfigurationType;
import com.eucalyptus.autoscaling.common.PutScalingPolicyResponseType;
import com.eucalyptus.autoscaling.common.PutScalingPolicyType;
import com.eucalyptus.autoscaling.common.PutScheduledUpdateGroupActionResponseType;
import com.eucalyptus.autoscaling.common.PutScheduledUpdateGroupActionType;
import com.eucalyptus.autoscaling.common.ResumeProcessesResponseType;
import com.eucalyptus.autoscaling.common.ResumeProcessesType;
import com.eucalyptus.autoscaling.common.ScalingPolicyType;
import com.eucalyptus.autoscaling.common.SetDesiredCapacityResponseType;
import com.eucalyptus.autoscaling.common.SetDesiredCapacityType;
import com.eucalyptus.autoscaling.common.SetInstanceHealthResponseType;
import com.eucalyptus.autoscaling.common.SetInstanceHealthType;
import com.eucalyptus.autoscaling.common.SuspendProcessesResponseType;
import com.eucalyptus.autoscaling.common.SuspendProcessesType;
import com.eucalyptus.autoscaling.common.TerminateInstanceInAutoScalingGroupResponseType;
import com.eucalyptus.autoscaling.common.TerminateInstanceInAutoScalingGroupType;
import com.eucalyptus.autoscaling.common.UpdateAutoScalingGroupResponseType;
import com.eucalyptus.autoscaling.common.UpdateAutoScalingGroupType;
import com.eucalyptus.autoscaling.configurations.LaunchConfiguration;
import com.eucalyptus.autoscaling.configurations.LaunchConfigurations;
import com.eucalyptus.autoscaling.configurations.PersistenceLaunchConfigurations;
import com.eucalyptus.autoscaling.groups.AutoScalingGroup;
import com.eucalyptus.autoscaling.groups.AutoScalingGroups;
import com.eucalyptus.autoscaling.groups.HealthCheckType;
import com.eucalyptus.autoscaling.groups.PersistenceAutoScalingGroups;
import com.eucalyptus.autoscaling.groups.TerminationPolicyType;
import com.eucalyptus.autoscaling.metadata.AutoScalingMetadataException;
import com.eucalyptus.autoscaling.metadata.AutoScalingMetadataNotFoundException;
import com.eucalyptus.autoscaling.policies.AdjustmentType;
import com.eucalyptus.autoscaling.policies.PersistenceScalingPolicies;
import com.eucalyptus.autoscaling.policies.ScalingPolicies;
import com.eucalyptus.autoscaling.policies.ScalingPolicy;
import com.eucalyptus.context.Context;
import com.eucalyptus.context.Contexts;
import com.eucalyptus.util.Callback;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.Exceptions;
import com.eucalyptus.util.Numbers;
import com.eucalyptus.util.OwnerFullName;
import com.eucalyptus.util.RestrictedTypes;
import com.eucalyptus.util.Strings;
import com.eucalyptus.util.TypeMappers;
import com.google.common.base.Enums;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class AutoScalingService {
  private static final Logger logger = Logger.getLogger( AutoScalingService.class );
  private final LaunchConfigurations launchConfigurations;
  private final AutoScalingGroups autoScalingGroups;
  private final ScalingPolicies scalingPolicies;
  
  public AutoScalingService() {
    this( 
        new PersistenceLaunchConfigurations( ),
        new PersistenceAutoScalingGroups( ),
        new PersistenceScalingPolicies( ) );
  }

  protected AutoScalingService( final LaunchConfigurations launchConfigurations,
                                final AutoScalingGroups autoScalingGroups,
                                final ScalingPolicies scalingPolicies ) {
    this.launchConfigurations = launchConfigurations;
    this.autoScalingGroups = autoScalingGroups;
    this.scalingPolicies = scalingPolicies;
  }

  public DescribeAutoScalingGroupsResponseType describeAutoScalingGroups( final DescribeAutoScalingGroupsType request ) throws EucalyptusCloudException {
    final DescribeAutoScalingGroupsResponseType reply = request.getReply( );

    //TODO:STEVE: MaxRecords / NextToken support for DescribeAutoScalingGroups

    final Context ctx = Contexts.lookup( );
    final boolean showAll = request.autoScalingGroupNames().remove( "verbose" );
    final OwnerFullName ownerFullName = ctx.hasAdministrativePrivileges( ) &&  showAll ?
        null :
        ctx.getUserFullName( ).asAccountFullName( );

    final Predicate<AutoScalingGroupMetadata> requestedAndAccessible = 
        AutoScalingMetadatas.filterPrivilegesByIdOrArn( request.autoScalingGroupNames() );

    try {
      final List<AutoScalingGroupType> results = reply.getDescribeAutoScalingGroupsResult().getAutoScalingGroups().getMember();
      for ( final AutoScalingGroup autoScalingGroup : autoScalingGroups.list( ownerFullName, requestedAndAccessible ) ) {
        results.add( TypeMappers.transform( autoScalingGroup, AutoScalingGroupType.class ) );
      }
    } catch ( Exception e ) {
      handleException( e );
    }    
    
    return reply;
  }

  public EnableMetricsCollectionResponseType enableMetricsCollection(EnableMetricsCollectionType request) throws EucalyptusCloudException {
    EnableMetricsCollectionResponseType reply = request.getReply( );
    return reply;
  }

  public ResumeProcessesResponseType resumeProcesses(ResumeProcessesType request) throws EucalyptusCloudException {
    ResumeProcessesResponseType reply = request.getReply( );
    return reply;
  }

  public DeleteLaunchConfigurationResponseType deleteLaunchConfiguration( final DeleteLaunchConfigurationType request ) throws EucalyptusCloudException {
    final DeleteLaunchConfigurationResponseType reply = request.getReply( );
    final Context ctx = Contexts.lookup( );
    try {
      final LaunchConfiguration launchConfiguration = launchConfigurations.lookup( 
          ctx.getUserFullName( ).asAccountFullName( ), 
          request.getLaunchConfigurationName( ) );
      if ( RestrictedTypes.filterPrivileged().apply( launchConfiguration ) ) {
        launchConfigurations.delete( launchConfiguration );
      } // else treat this as though the configuration does not exist
    } catch ( AutoScalingMetadataNotFoundException e ) {
      // so nothing to delete, move along      
    } catch ( Exception e ) {
      handleException( e );
    }    
    return reply;
  }

  public DescribePoliciesResponseType describePolicies(final DescribePoliciesType request) throws EucalyptusCloudException {
    final DescribePoliciesResponseType reply = request.getReply( );

    //TODO:STEVE: MaxRecords / NextToken support for DescribePolicies

    final Context ctx = Contexts.lookup( );
    final boolean showAll = request.policyNames().remove( "verbose" );
    final OwnerFullName ownerFullName = ctx.hasAdministrativePrivileges( ) &&  showAll ?
        null :
        ctx.getUserFullName( ).asAccountFullName( );

    try {
      final Predicate<ScalingPolicy> requestedAndAccessible =
        Predicates.and( 
          AutoScalingMetadatas.filterPrivilegesByIdOrArn( request.policyNames() ),
          AutoScalingResourceName.isResourceName().apply( request.getAutoScalingGroupName() ) ?
            AutoScalingMetadatas.filterByProperty(
                  AutoScalingResourceName.parse( request.getAutoScalingGroupName(), autoScalingGroup ).getUuid(),
                  ScalingPolicies.toGroupUuid() )  :
            AutoScalingMetadatas.filterByProperty( 
                request.getAutoScalingGroupName(), 
                ScalingPolicies.toGroupName() )
        );

      final List<ScalingPolicyType> results = reply.getDescribePoliciesResult().getScalingPolicies().getMember();
      for ( final ScalingPolicy scalingPolicy : scalingPolicies.list( ownerFullName, requestedAndAccessible ) ) {
        results.add( TypeMappers.transform( scalingPolicy, ScalingPolicyType.class ) );
      }
    } catch ( Exception e ) {
      handleException( e );
    }

    return reply;
  }

  public DescribeScalingProcessTypesResponseType describeScalingProcessTypes(DescribeScalingProcessTypesType request) throws EucalyptusCloudException {
    DescribeScalingProcessTypesResponseType reply = request.getReply( );
    return reply;
  }

  public CreateAutoScalingGroupResponseType createAutoScalingGroup( final CreateAutoScalingGroupType request ) throws EucalyptusCloudException {
    final CreateAutoScalingGroupResponseType reply = request.getReply( );

    final Context ctx = Contexts.lookup( );
    final Supplier<AutoScalingGroup> allocator = new Supplier<AutoScalingGroup>( ) {
      @Override
      public AutoScalingGroup get( ) {
        try {
          final AutoScalingGroups.PersistingBuilder builder = autoScalingGroups.create(
              ctx.getUserFullName(),
              request.getAutoScalingGroupName(),
              launchConfigurations.lookup( ctx.getUserFullName().asAccountFullName(), request.getLaunchConfigurationName() ),
              Numbers.intValue( request.getMinSize() ),
              Numbers.intValue( request.getMaxSize() ) )
              .withAvailabilityZones( request.availabilityZones() )
              .withDefaultCooldown( Numbers.intValue( request.getDefaultCooldown() ) )
              .withDesiredCapacity( Numbers.intValue( request.getDesiredCapacity() ) )
              .withHealthCheckGracePeriod( Numbers.intValue( request.getHealthCheckGracePeriod() ) )
              .withHealthCheckType(
                  request.getHealthCheckType()==null ? null : HealthCheckType.valueOf( request.getHealthCheckType() ) )
              .withLoadBalancerNames( request.loadBalancerNames() )
              .withTerminationPolicyTypes( request.terminationPolicies() == null ? null :
                  Collections2.filter( Collections2.transform( 
                    request.terminationPolicies(), Enums.valueOfFunction( TerminationPolicyType.class) ),
                    Predicates.not( Predicates.isNull() ) ) );

          //TODO:STEVE: input validation
          return builder.persist();
        } catch ( AutoScalingMetadataNotFoundException e ) {
          throw Exceptions.toUndeclared( new InvalidParameterValueException( "Launch configuration not found: " + request.getLaunchConfigurationName() ) );
        } catch ( IllegalArgumentException e ) {
          throw Exceptions.toUndeclared( new InvalidParameterValueException( "Invalid health check type: " + request.getHealthCheckType() ) );
        } catch ( Exception ex ) {
          throw new RuntimeException( ex );
        }
      }
    };

    try {
      RestrictedTypes.allocateUnitlessResource( allocator );
    } catch ( Exception e ) {
      handleException( e );
    }

    return reply;
  }

  public DescribeScalingActivitiesResponseType describeScalingActivities(DescribeScalingActivitiesType request) throws EucalyptusCloudException {
    DescribeScalingActivitiesResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeNotificationConfigurationsResponseType describeNotificationConfigurations(DescribeNotificationConfigurationsType request) throws EucalyptusCloudException {
    DescribeNotificationConfigurationsResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeTerminationPolicyTypesResponseType describeTerminationPolicyTypes( final DescribeTerminationPolicyTypesType request ) throws EucalyptusCloudException {
    final DescribeTerminationPolicyTypesResponseType reply = request.getReply( );    
    
    final List<String> policies = reply.getDescribeTerminationPolicyTypesResult().getTerminationPolicyTypes().getMember();
    policies.addAll( Collections2.transform( 
        Collections2.filter( EnumSet.allOf( TerminationPolicyType.class ), RestrictedTypes.filterPrivilegedWithoutOwner() ), 
        Strings.toStringFunction() ) );
    
    return reply;
  }

  public DescribeTagsResponseType describeTags(DescribeTagsType request) throws EucalyptusCloudException {
    DescribeTagsResponseType reply = request.getReply( );
    return reply;
  }

  public ExecutePolicyResponseType executePolicy(final ExecutePolicyType request) throws EucalyptusCloudException {
    final ExecutePolicyResponseType reply = request.getReply( );
    
    //TODO:STEVE: cooldown support

    final Context ctx = Contexts.lookup( );
    final AccountFullName accountFullName = ctx.getUserFullName( ).asAccountFullName( );
    try {
      final ScalingPolicy scalingPolicy;
      try {
        scalingPolicy = scalingPolicies.lookup( 
          accountFullName,
          request.getAutoScalingGroupName(),
          request.getPolicyName() );        
      } catch ( AutoScalingMetadataNotFoundException e ) {
        throw new InvalidParameterValueException( "Scaling policy not found: " + request.getPolicyName() );
      } 
      
      autoScalingGroups.update( accountFullName, request.getAutoScalingGroupName(), new Callback<AutoScalingGroup>(){
        @Override
        public void fire( final AutoScalingGroup autoScalingGroup ) {          
          autoScalingGroup.setDesiredCapacity( scalingPolicy.getAdjustmentType().adjustCapacity( 
            autoScalingGroup.getDesiredCapacity(), //TODO:STEVE: should be actual capacity ...
            scalingPolicy.getScalingAdjustment(),
            Objects.firstNonNull( scalingPolicy.getMinAdjustmentStep(), 0),
            Objects.firstNonNull( autoScalingGroup.getMinSize(), 0 ),
            Objects.firstNonNull( autoScalingGroup.getMaxSize(), Integer.MAX_VALUE )
          ) );
        }
      } );
    } catch( Exception e ) {
      handleException( e );
    }    
    
    return reply;
  }

  public DeleteTagsResponseType deleteTags(DeleteTagsType request) throws EucalyptusCloudException {
    DeleteTagsResponseType reply = request.getReply( );
    return reply;
  }

  public PutScalingPolicyResponseType putScalingPolicy(final PutScalingPolicyType request) throws EucalyptusCloudException {
    final PutScalingPolicyResponseType reply = request.getReply( );

    final Context ctx = Contexts.lookup( );
    final AccountFullName accountFullName = ctx.getUserFullName( ).asAccountFullName();
    try {
      // Try update
      final ScalingPolicy scalingPolicy = scalingPolicies.update(
          accountFullName,
          request.getAutoScalingGroupName(),
          request.getPolicyName(), new Callback<ScalingPolicy>() {
        @Override
        public void fire( final ScalingPolicy scalingPolicy ) {
          if ( RestrictedTypes.filterPrivileged().apply( scalingPolicy ) ) {
            //TODO:STEVE: input validation
            // You will get a ValidationError if you use MinAdjustmentStep on a policy with an AdjustmentType other than PercentChangeInCapacity. 
            if ( request.getAdjustmentType() != null )
              scalingPolicy.setAdjustmentType( 
                  Enums.valueOfFunction( AdjustmentType.class ).apply( request.getAdjustmentType() ) );
            if ( request.getScalingAdjustment() != null )
              scalingPolicy.setScalingAdjustment( request.getScalingAdjustment() );
            if ( request.getCooldown() != null )
              scalingPolicy.setCooldown( request.getCooldown() );
            if ( request.getMinAdjustmentStep() != null )
              scalingPolicy.setMinAdjustmentStep( request.getMinAdjustmentStep() );
          }
        }
      } );
      reply.getPutScalingPolicyResult().setPolicyARN( scalingPolicy.getArn() );
    } catch ( AutoScalingMetadataNotFoundException e ) {
      // Not found, create
      final Supplier<ScalingPolicy> allocator = new Supplier<ScalingPolicy>( ) {
        @Override
        public ScalingPolicy get( ) {
          try {
            final ScalingPolicies.PersistingBuilder builder = scalingPolicies.create(
                ctx.getUserFullName( ),
                autoScalingGroups.lookup( accountFullName, request.getAutoScalingGroupName() ),
                request.getPolicyName(),
                Enums.valueOfFunction( AdjustmentType.class ).apply( request.getAdjustmentType() ),
                request.getScalingAdjustment() )
                .withCooldown( request.getCooldown() )
                .withMinAdjustmentStep( request.getMinAdjustmentStep() );

            //TODO:STEVE: input validation
            // No Auto Scaling name, including policy names, can contain the colon (:) character because colons serve as delimiters in ARNs.
            // You will get a ValidationError if you use MinAdjustmentStep on a policy with an AdjustmentType other than PercentChangeInCapacity. 
            return builder.persist();
          } catch ( AutoScalingMetadataNotFoundException e ) {
            throw Exceptions.toUndeclared( new InvalidParameterValueException( "Auto scaling group not found: " + request.getAutoScalingGroupName() ) );
          } catch ( IllegalArgumentException e ) {
            throw Exceptions.toUndeclared( new InvalidParameterValueException( "Invalid adjustment type: " + request.getAdjustmentType() ) );
          } catch ( Exception ex ) {
            throw new RuntimeException( ex );
          }
        }
      };

      try {
        final ScalingPolicy scalingPolicy = RestrictedTypes.allocateUnitlessResource( allocator );
        reply.getPutScalingPolicyResult().setPolicyARN( scalingPolicy.getArn() );
      } catch ( Exception exception ) {
        handleException( exception );
      }

    } catch ( Exception e ) {
      handleException( e );
    }

    return reply;
  }

  public PutNotificationConfigurationResponseType putNotificationConfiguration(PutNotificationConfigurationType request) throws EucalyptusCloudException {
    PutNotificationConfigurationResponseType reply = request.getReply( );
    return reply;
  }

  public DeletePolicyResponseType deletePolicy(final DeletePolicyType request) throws EucalyptusCloudException {
    final DeletePolicyResponseType reply = request.getReply( );
    final Context ctx = Contexts.lookup( );
    try {
      final ScalingPolicy scalingPolicy = scalingPolicies.lookup(
          ctx.getUserFullName( ).asAccountFullName( ),
          request.getAutoScalingGroupName(),
          request.getPolicyName( ) );
      if ( RestrictedTypes.filterPrivileged().apply( scalingPolicy ) ) {
        scalingPolicies.delete( scalingPolicy );
      } // else treat this as though the configuration does not exist
    } catch ( AutoScalingMetadataNotFoundException e ) {
      // so nothing to delete, move along      
    } catch ( Exception e ) {
      handleException( e );
    }
    return reply;
  }

  public DeleteNotificationConfigurationResponseType deleteNotificationConfiguration(DeleteNotificationConfigurationType request) throws EucalyptusCloudException {
    DeleteNotificationConfigurationResponseType reply = request.getReply( );
    return reply;
  }

  public DeleteScheduledActionResponseType deleteScheduledAction(DeleteScheduledActionType request) throws EucalyptusCloudException {
    DeleteScheduledActionResponseType reply = request.getReply( );
    return reply;
  }

  public SetInstanceHealthResponseType setInstanceHealth(SetInstanceHealthType request) throws EucalyptusCloudException {
    SetInstanceHealthResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeAutoScalingNotificationTypesResponseType describeAutoScalingNotificationTypes(DescribeAutoScalingNotificationTypesType request) throws EucalyptusCloudException {
    DescribeAutoScalingNotificationTypesResponseType reply = request.getReply( );
    return reply;
  }

  public CreateOrUpdateTagsResponseType createOrUpdateTags(CreateOrUpdateTagsType request) throws EucalyptusCloudException {
    CreateOrUpdateTagsResponseType reply = request.getReply( );
    return reply;
  }

  public SuspendProcessesResponseType suspendProcesses(SuspendProcessesType request) throws EucalyptusCloudException {
    SuspendProcessesResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeAutoScalingInstancesResponseType describeAutoScalingInstances(DescribeAutoScalingInstancesType request) throws EucalyptusCloudException {
    DescribeAutoScalingInstancesResponseType reply = request.getReply( );
    return reply;
  }

  public CreateLaunchConfigurationResponseType createLaunchConfiguration( final CreateLaunchConfigurationType request ) throws EucalyptusCloudException {
    final CreateLaunchConfigurationResponseType reply = request.getReply( );
    final Context ctx = Contexts.lookup( );
    final Supplier<LaunchConfiguration> allocator = new Supplier<LaunchConfiguration>( ) {
      @Override
      public LaunchConfiguration get( ) {
        try {
          final LaunchConfigurations.PersistingBuilder builder = launchConfigurations.create(
              ctx.getUserFullName(),
              request.getLaunchConfigurationName(),
              request.getImageId(),
              request.getInstanceType() )
            .withKernelId( request.getKernelId() )
            .withRamdiskId( request.getRamdiskId() )
            .withKeyName( request.getKeyName() )
            .withUserData( request.getUserData() )
            .withInstanceMonitoring( request.getInstanceMonitoring() != null ? request.getInstanceMonitoring().getEnabled() : null )
            .withSecurityGroups( request.getSecurityGroups() != null ? request.getSecurityGroups().getMember() : null );          
            
          if ( request.getBlockDeviceMappings() != null ) {
            for ( final BlockDeviceMappingType blockDeviceMappingType : request.getBlockDeviceMappings().getMember() ) {
              builder.withBlockDeviceMapping( 
                  blockDeviceMappingType.getDeviceName(),
                  blockDeviceMappingType.getVirtualName(),
                  blockDeviceMappingType.getEbs() != null ? blockDeviceMappingType.getEbs().getSnapshotId() : null,
                  blockDeviceMappingType.getEbs() != null ? Numbers.intValue( blockDeviceMappingType.getEbs().getVolumeSize() ) : null ); 
            }
          }
          
          //TODO:STEVE: input validation
          return builder.persist();
        } catch ( Exception ex ) {
          throw new RuntimeException( ex );
        }
      }
    };

    try {
      RestrictedTypes.allocateUnitlessResource( allocator );
    } catch ( Exception e ) {
      handleException( e );
    }

    return reply;
  }

  public DeleteAutoScalingGroupResponseType deleteAutoScalingGroup( final DeleteAutoScalingGroupType request ) throws EucalyptusCloudException {
    final DeleteAutoScalingGroupResponseType reply = request.getReply( );

    final Context ctx = Contexts.lookup( );
    try {
      final AutoScalingGroup autoScalingGroup = autoScalingGroups.lookup(
          ctx.getUserFullName( ).asAccountFullName( ),
          request.getAutoScalingGroupName() );
      if ( RestrictedTypes.filterPrivileged().apply( autoScalingGroup ) ) {
        autoScalingGroups.delete( autoScalingGroup );
      } // else treat this as though the group does not exist
    } catch ( AutoScalingMetadataNotFoundException e ) {
      // so nothing to delete, move along      
    } catch ( Exception e ) {
      handleException( e );
    }
    
    return reply;
  }

  public DisableMetricsCollectionResponseType disableMetricsCollection(DisableMetricsCollectionType request) throws EucalyptusCloudException {
    DisableMetricsCollectionResponseType reply = request.getReply( );
    return reply;
  }

  public UpdateAutoScalingGroupResponseType updateAutoScalingGroup( final UpdateAutoScalingGroupType request ) throws EucalyptusCloudException {
    final UpdateAutoScalingGroupResponseType reply = request.getReply( );

    final Context ctx = Contexts.lookup( );
    try {
      final AccountFullName accountFullName = ctx.getUserFullName().asAccountFullName();
      final Callback<AutoScalingGroup> groupCallback = new Callback<AutoScalingGroup>() {
        @Override
        public void fire( final AutoScalingGroup autoScalingGroup ) {
          if ( RestrictedTypes.filterPrivileged().apply( autoScalingGroup ) ) {
            if ( request.availabilityZones() != null && !request.availabilityZones().isEmpty() )
              autoScalingGroup.setAvailabilityZones( Sets.newHashSet( request.availabilityZones() ) );
            if ( request.getDefaultCooldown() != null )
              autoScalingGroup.setDefaultCooldown( Numbers.intValue( request.getDefaultCooldown() ) );
            if ( request.getDesiredCapacity() != null )
              autoScalingGroup.setDesiredCapacity( Numbers.intValue( request.getDesiredCapacity() ) );
            if ( request.getHealthCheckGracePeriod() != null )
              autoScalingGroup.setHealthCheckGracePeriod( Numbers.intValue( request.getHealthCheckGracePeriod() ) );
            if ( request.getHealthCheckType() != null )
              autoScalingGroup.setHealthCheckType( Enums.valueOfFunction( HealthCheckType.class ).apply( request.getHealthCheckType() ) );
            if ( request.getLaunchConfigurationName() != null )
              try {
                autoScalingGroup.setLaunchConfiguration( launchConfigurations.lookup( accountFullName, request.getLaunchConfigurationName() ) );
              } catch ( AutoScalingMetadataNotFoundException e ) {
                throw Exceptions.toUndeclared( new InvalidParameterValueException( "Launch configuration not found: " + request.getLaunchConfigurationName() ) );
              } catch ( AutoScalingMetadataException e ) {
                throw Exceptions.toUndeclared( e );
              }
            if ( request.getMaxSize() != null )
              autoScalingGroup.setMaxSize( Numbers.intValue( request.getMaxSize() ) );
            if ( request.getMinSize() != null )
              autoScalingGroup.setMinSize( Numbers.intValue( request.getMinSize() ) );
            if ( request.terminationPolicies() != null && !request.terminationPolicies().isEmpty() )
              autoScalingGroup.setTerminationPolicies( Sets.newHashSet( Iterables.filter( Iterables.transform(
                  request.terminationPolicies(), Enums.valueOfFunction( TerminationPolicyType.class ) ),
                  Predicates.not( Predicates.isNull() ) ) ) );
            //TODO:STEVE: something for VPC zone identifier or placement group?
          }
        }
      };

      autoScalingGroups.update(
          accountFullName,
          request.getAutoScalingGroupName(),
          groupCallback);
    } catch ( AutoScalingMetadataNotFoundException e ) {
      throw new InvalidParameterValueException( "Auto scaling group not found: " + request.getAutoScalingGroupName() );
    } catch ( Exception e ) {
      handleException( e );
    }
    
    return reply;
  }

  public DescribeLaunchConfigurationsResponseType describeLaunchConfigurations(DescribeLaunchConfigurationsType request) throws EucalyptusCloudException {
    final DescribeLaunchConfigurationsResponseType reply = request.getReply( );

    //TODO:STEVE: MaxRecords / NextToken support for DescribeLaunchConfigurations
    
    final Context ctx = Contexts.lookup( );
    final boolean showAll = request.launchConfigurationNames().remove( "verbose" );  
    final OwnerFullName ownerFullName = ctx.hasAdministrativePrivileges( ) &&  showAll ? 
        null : 
        ctx.getUserFullName( ).asAccountFullName( );

    final Predicate<LaunchConfigurationMetadata> requestedAndAccessible =
        AutoScalingMetadatas.filterPrivilegesByIdOrArn( request.launchConfigurationNames() );

    try {
      final List<LaunchConfigurationType> results = reply.getDescribeLaunchConfigurationsResult( ).getLaunchConfigurations().getMember();
      for ( final LaunchConfiguration launchConfiguration : launchConfigurations.list( ownerFullName, requestedAndAccessible ) ) {
        results.add( TypeMappers.transform( launchConfiguration, LaunchConfigurationType.class ) );
      }
    } catch ( Exception e ) {
      handleException( e );
    }

    return reply;
  }

  public DescribeAdjustmentTypesResponseType describeAdjustmentTypes(final DescribeAdjustmentTypesType request) throws EucalyptusCloudException {
    final DescribeAdjustmentTypesResponseType reply = request.getReply( );

    reply.getDescribeAdjustmentTypesResult().setAdjustmentTypes(
        Collections2.transform(
            Collections2.filter( EnumSet.allOf( AdjustmentType.class ), RestrictedTypes.filterPrivilegedWithoutOwner() ),
            Strings.toStringFunction() ) );

    return reply;
  }

  public DescribeScheduledActionsResponseType describeScheduledActions(DescribeScheduledActionsType request) throws EucalyptusCloudException {
    DescribeScheduledActionsResponseType reply = request.getReply( );
    return reply;
  }

  public PutScheduledUpdateGroupActionResponseType putScheduledUpdateGroupAction(PutScheduledUpdateGroupActionType request) throws EucalyptusCloudException {
    PutScheduledUpdateGroupActionResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeMetricCollectionTypesResponseType describeMetricCollectionTypes(DescribeMetricCollectionTypesType request) throws EucalyptusCloudException {
    DescribeMetricCollectionTypesResponseType reply = request.getReply( );
    return reply;
  }

  public SetDesiredCapacityResponseType setDesiredCapacity( final SetDesiredCapacityType request ) throws EucalyptusCloudException {
    final SetDesiredCapacityResponseType reply = request.getReply( );

    final Context ctx = Contexts.lookup( );
    try {
      final Callback<AutoScalingGroup> groupCallback = new Callback<AutoScalingGroup>() {
        @Override
        public void fire( final AutoScalingGroup autoScalingGroup ) {
          if ( RestrictedTypes.filterPrivileged().apply( autoScalingGroup ) ) {
            autoScalingGroup.setDesiredCapacity( Numbers.intValue( request.getDesiredCapacity() ) );
          } 
        }
      };
      
      autoScalingGroups.update(
          ctx.getUserFullName().asAccountFullName(),
          request.getAutoScalingGroupName(),
          groupCallback);
    } catch ( AutoScalingMetadataNotFoundException e ) {
      throw new InvalidParameterValueException( "Auto scaling group not found: " + request.getAutoScalingGroupName() );
    } catch ( Exception e ) {
      handleException( e );
    }    
    
    return reply;
  }

  public TerminateInstanceInAutoScalingGroupResponseType terminateInstanceInAutoScalingGroup(TerminateInstanceInAutoScalingGroupType request) throws EucalyptusCloudException {
    TerminateInstanceInAutoScalingGroupResponseType reply = request.getReply( );
    return reply;
  }

  private static void handleException( final Exception e ) throws AutoScalingException {
    final AutoScalingException cause = Exceptions.findCause( e, AutoScalingException.class );
    if ( cause != null ) {
      throw cause;
    }
    
    final AuthQuotaException quotaCause = Exceptions.findCause( e, AuthQuotaException.class );
    if ( quotaCause != null ) {
      throw new LimitExceededException( "Request would exceed quota for type: " + quotaCause.getType() );
    }
    
    final ConstraintViolationException constraintViolationException = 
        Exceptions.findCause( e, ConstraintViolationException.class );
    if ( constraintViolationException != null ) {
      throw new AlreadyExistsException( "Resource already exists" );
    }

    final InvalidResourceNameException invalidResourceNameException =
        Exceptions.findCause( e, InvalidResourceNameException.class );
    if ( invalidResourceNameException != null ) {
      throw new InvalidParameterValueException( invalidResourceNameException.getMessage() );
    }

    logger.error( e, e );

    final InternalFailureException exception = new InternalFailureException( String.valueOf(e.getMessage()) );
    if ( Contexts.lookup( ).hasAdministrativePrivileges() ) {
      exception.initCause( e );      
    }
    throw exception;
  }
}
