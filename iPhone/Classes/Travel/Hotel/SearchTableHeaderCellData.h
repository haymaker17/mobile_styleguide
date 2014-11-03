//
//  SearchTableHeaderCellData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/1/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractTableViewCellData.h"

@interface SearchTableHeaderCellData : AbstractTableViewCellData

@property (nonatomic, strong) NSString *location;
@property (nonatomic, strong) NSString *stayDatesString;
@property (strong, nonatomic) NSString *imageName;

@end
