//
//  OptionsSelectVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "OptionsSelectDelegate.h"

@interface OptionsSelectVC : MobileViewController
{
    
    id<OptionsSelectDelegate>	__weak _delegate;

    UITableView					*tableList;

    NSString					*optionTitle;
    NSArray						*labels;
    NSArray                     *items;
    int							selectedRowIndex;
    CGFloat						preferredFontSize;
    
    NSObject                    *identifier;  // a way to identify the source of this options select
}

@property (nonatomic, weak) id<OptionsSelectDelegate>     delegate;

@property (nonatomic, strong) IBOutlet UITableView			*tableList;

@property (nonatomic, strong) NSObject						*identifier;
@property (nonatomic, strong) NSString						*optionTitle;
@property (nonatomic, strong) NSArray						*labels;
@property (nonatomic, strong) NSArray						*items;
@property (nonatomic) int									selectedRowIndex;
@property (nonatomic) CGFloat								preferredFontSize;

@end
