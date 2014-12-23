//
//  ProfileViewController.h
//  ConcurMobile
//
//  Created by Ray Chi on 11/13/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEProfile.h"

@interface ProfileViewController : UITableViewController <UITextFieldDelegate,UITableViewDataSource>

- (instancetype)initWithTitle;

- (IBAction)btnCloseClick:(id)sender;
- (IBAction)btnSaveClick:(id)sender;



@end
