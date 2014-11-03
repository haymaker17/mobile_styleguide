//
//  GoGoNotificationHandler.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GoGoOffer.h"
#import "NotificationHandler.h"

@interface GoGoNotificationHandler : NotificationHandler

- (void)processOffer:(GoGoOffer *)offer
 forApplicationState:(UIApplicationState)applicationState;

@end
