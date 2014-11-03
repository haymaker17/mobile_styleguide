//
//  QuickExpenseDataSource.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormVCBaseInline.h"

@interface QuickExpenseDataSource : NSObject <FormInlineDelegate>

@property (nonatomic,strong) UITableView *formTableView;

@end
