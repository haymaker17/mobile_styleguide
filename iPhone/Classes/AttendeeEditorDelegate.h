//
//  AttendeeEditorDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/7/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class AttendeeData;

@protocol AttendeeEditorDelegate

@optional
-(void)editedAttendee:(AttendeeData*)attendee createdByEditor:(BOOL)created;

@required
-(BOOL)canEdit;
@end
