//
//  MoreMenuSectionData.m
//  ConcurMobile
//
//  Created by ernest cho on 3/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MoreMenuSectionData.h"
#import "MoreMenuRowData.h"

@interface MoreMenuSectionData()

@property (nonatomic, strong) NSMutableArray *rows;

@end

@implementation MoreMenuSectionData

@synthesize sectionTitle, rows;

- (id)init
{
    self = [super init];
    if (self != nil) {
        self.rows = [[NSMutableArray alloc] init];
    }
    return self;
}

- (void) saveRowData:(NSString *)rowLabel withImage:(NSString *)imageName withTag:(NSInteger)tag
{
    UIImage *img = [UIImage imageNamed:imageName];
    
    MoreMenuRowData *row = [[MoreMenuRowData alloc] init];
    row.rowLabel = rowLabel;
    row.image = img;
    row.tag = tag;
    [self.rows addObject:row];
}

- (NSInteger)getRowCount
{
    return [rows count];
}

- (NSString *)getTextForRow:(NSInteger)rowIndex
{
    return [self getRow:rowIndex].rowLabel;
}

- (UIImage *)getImageForRow:(NSInteger)rowIndex
{
    return [self getRow:rowIndex].image;
}

- (NSInteger)getTagForRow:(NSInteger)rowIndex
{
    return [self getRow:rowIndex].tag;
}

- (MoreMenuRowData *)getRow:(NSInteger)rowIndex
{
    return rows[rowIndex];
}

@end
