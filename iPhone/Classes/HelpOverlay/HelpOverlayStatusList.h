//
//  HelpOverlayStatusList.h
//  ConcurMobile
//
//  Created by ernest cho on 10/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HelpOverlayStatusList : NSObject

+ (id)sharedList;
- (BOOL)isOverlayDisabled:(NSString *)overlayName;
- (void)disableOverlay:(NSString *)overlayName;

- (void)clearStatusForOverlay:(NSString *)overlayName;

@end
