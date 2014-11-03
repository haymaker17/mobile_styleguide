//
//  LoginHelpContentsVC.h
//  ConcurMobile
//
//  Created by charlottef on 12/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "LoginCreatePinVC.h"
#import "EditInlineCell.h"
#import "EditInlineCellDelegate.h"
#import "Config.h"
#import "ResetPinUserEmailData.h"

@interface LoginHelpContentsVC : MobileViewController <UITableViewDataSource, UITableViewDelegate, EditInlineCellDelegate >
{
    UITableView *tableList;
    NSArray     *topics;
    UIButton    *btnMoreHelp;
}

@property (strong, nonatomic) IBOutlet UITableView  *tableList;
@property (strong, nonatomic) IBOutlet NSArray      *topics;


- (IBAction)btnSubmitPressed:(id)sender;


@end
