//
//  DateUtils.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DateUtils : NSObject

+ (NSString *)dateFormattedForMWS:(NSDate *)date;
+ (NSString *)dateFormattedForUI:(NSDate *)date;

@end
