//
//  LoginCreatePinVC.h
//  ConcurMobile
//
//  Created by Sally Yan on 6/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EditInlineCell.h"
#import "ApplicationLock.h"
#import "ResetUserPin.h"
#import "LoginViewController.h"
#import "UIView+FindAndResignFirstResponder.h"

@interface LoginCreatePinVC : MobileViewController < UITableViewDataSource, UITableViewDelegate, EditInlineCellDelegate>
{
    UITableView *tableList;
}

@property (nonatomic, strong) IBOutlet UITableView *tableList;

@end
