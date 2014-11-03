//
//  MoreMenuSectionData.h
//  ConcurMobile
//
//  Created by ernest cho on 3/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MoreMenuSectionData : NSObject

@property (nonatomic, strong) NSString *sectionTitle;

- (void)saveRowData:(NSString *)rowLabel withImage:(NSString *)imageName withTag:(NSInteger)tag;

- (NSInteger)getRowCount;
- (NSString *)getTextForRow:(NSInteger)rowIndex;
- (UIImage *)getImageForRow:(NSInteger)rowIndex;
- (NSInteger)getTagForRow:(NSInteger)rowIndex;

@end
