//
//  UIDevice+Additions.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 9/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIDevice (Additions)

+ (BOOL)isPad;
+ (BOOL)isPhone;

@end


@interface NSString (UIDeviceAdditions)

/*
 * append _iPhone or _iPad according to the device we are running on
 */
- (NSString *)storyboardName;

@end