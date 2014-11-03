//
//  Fusion2014TextSearchViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion2014TextSearchViewController : UIViewController <UITableViewDataSource, UITableViewDelegate, UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet UITextField *txtSearchField;
@property (weak, nonatomic) IBOutlet UIButton *helpButton;

@property (assign) EvaSearchCategory category;


@end
