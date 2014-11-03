//
//  AppRating.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"

// MOB-8074 use AppRating as proxy to mvc responding to app rating alert view
@interface AppRating : NSObject<UIAlertViewDelegate> {
    MobileViewController    *mvc;
}

@property (nonatomic, weak) MobileViewController* mvc;
-(void) startListeningToCancellationNotifications;
-(void) stopListeningToCancellationNotifications;
-(void)receivedCancellationNotification:(NSNotification*)notification;

+ (AppRating*) sharedInstance;

+(void)gotoAppStoreRatings;

+(void)offerChoiceToRateApp:(MobileViewController *)vc alertTag:(int)alertTag;
+(BOOL) dateDiffSaysPrompt;
+(void)processChoiceToRateApp:(int)buttonIndex vc:(MobileViewController *)vc alertTag:(int)alertTag;
@end
