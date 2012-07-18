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

package com.eucalyptus.auth.policy.condition;

import java.text.ParseException;
import org.apache.log4j.Logger;
import com.eucalyptus.auth.policy.key.Iso8601DateParser;

@PolicyCondition( { Conditions.DATELESSTHAN, Conditions.DATELESSTHAN_S } )
public class DateLessThan implements DateConditionOp {
  
  private static final Logger LOG = Logger.getLogger( DateEquals.class );
  
  @Override
  public boolean check( String key, String value ) {
    try {
      return Iso8601DateParser.parse( key ).compareTo( Iso8601DateParser.parse( value ) ) < 0;
    } catch ( ParseException e ) {
      LOG.error( "Invalid input date input", e );
      return false;
    }
  }
  
}
