//
//  AttendeeActionDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@protocol AttendeeActionDelegate

-(void) attendeeListChanged;
-(NSArray*) getExcludedAtnTypeKeys;
-(NSString*) getAttendeeCrnCode;
@end
