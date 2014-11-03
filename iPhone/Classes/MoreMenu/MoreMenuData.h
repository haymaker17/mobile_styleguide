//
//  MoreMenuData.h
//  ConcurMobile
//
//  Created by ernest cho on 3/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIViewController.h>

@interface MoreMenuData : NSObject

// need the location of the select on iPad to show the actionsheet correctly.

- (BOOL)didSelectCell:(NSIndexPath *)indexPath withView:(UIViewController *)viewController atLocation:(CGRect)rect;

- (NSString *)getTitleForSection:(NSInteger)section;
- (NSInteger)numberOfSections;
- (NSInteger)numberOfRowsInSection:(NSInteger)section;

- (NSString *)getTextForCell:(NSIndexPath *)indexPath;
- (UIImage *)getImageForCell:(NSIndexPath *)indexPath;

@end
