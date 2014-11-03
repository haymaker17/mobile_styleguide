//
//  NotificationHandler.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NotificationHandler : NSObject

-(void) processNotificationEvent:(NotificationEvent *)event;
-(BOOL) requiresValidSession;

// Helper APIs
-(BOOL) isTopViewOfClass:(Class) theClass;

@end
