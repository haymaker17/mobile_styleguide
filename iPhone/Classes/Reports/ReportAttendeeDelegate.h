//
//  ReportAttendeeDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 5/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@protocol ReportAttendeeDelegate <NSObject>
-(void)attendeesEdited:(NSMutableArray*)editedAttendees;
-(BOOL)isParentEntry;
@end
