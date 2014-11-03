//
//  LoadingSpinnerCellData.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 8/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AbstractTableViewCellData.h"

@interface LoadingSpinnerCellData : AbstractTableViewCellData

@property (nonatomic,strong) NSString *loadingCaption;

- (instancetype)initWithCaption:(NSString *)loadingCaption;

@end
