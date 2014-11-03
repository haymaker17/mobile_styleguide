//
//  NotificationEvent.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 6/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NotificationEvent : NSObject

@property (nonatomic, strong) NSString              *type;
@property (nonatomic, strong) NSDictionary          *data; // Custom data for each notification type


@end
