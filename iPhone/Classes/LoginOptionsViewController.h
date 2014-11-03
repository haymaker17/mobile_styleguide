//
//  LoginOptionsViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EditInlineCell.h"
#import "LoginViewController.h"

@interface LoginOptionsViewController : MobileViewController <UITableViewDataSource, UITableViewDelegate, EditInlineCellDelegate>
{
    UITableView *tableList;
    NSString *txtCompanyCode;
    
    UIView *headerView;
    id<LoginDelegate> __weak loginDelegate;
}

@property int xOffset;
@property (nonatomic, weak) id<LoginDelegate> loginDelegate;
@property (nonatomic, strong) IBOutlet UITableView *tableList;
@property (nonatomic, strong) NSString *txtCompanyCode;
@property (nonatomic, strong) UIView *headerView;

@end
