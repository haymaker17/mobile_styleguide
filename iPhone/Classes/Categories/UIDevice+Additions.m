//
//  UIDevice+Additions.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 9/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "UIDevice+Additions.h"
#import "MCLogging.h"

@implementation UIDevice (Additions)

+ (BOOL)isPad{
    return [UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad;
}

+ (BOOL)isPhone{
    return [UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPhone;
}

@end


@implementation NSString (UIDeviceAdditions)

/*
 * append _iPhone or _iPad according to the device we are running on
 */
- (NSString *)storyboardName{
    if ([UIDevice isPhone]) {
        return [NSString stringWithFormat:@"%@_iPhone",self];
    }
    if ([UIDevice isPad]) {
        return [NSString stringWithFormat:@"%@_iPad",self];
    }
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"NSString storyboardName cannot figure out if we are on iPhone or iPad, default to iPhone for %@ ",self] Level:MC_LOG_ERRO];
    return [NSString stringWithFormat:@"%@_iPad",self];
}

@end