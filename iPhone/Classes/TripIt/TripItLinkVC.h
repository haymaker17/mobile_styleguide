//
//  TripItLinkVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EditInlineCell.h"
#import "MobileViewController.h"

@interface TripItLinkVC : MobileViewController <UITableViewDelegate, UITableViewDataSource>
{
    UITableView             *tableList;
    UIButton                *btnGreen;
    UIView                  *viewHeader;
    UILabel                 *lblHeader;
    NSString                *email, *pwd;
}

@property (nonatomic, strong) IBOutlet UITableView          *tableList;
@property (nonatomic, strong) IBOutlet UIView               *viewHeader;
@property (nonatomic, strong) IBOutlet UILabel              *lblHeader;
@property (nonatomic, strong) UIButton                      *btnGreen;

@property (nonatomic, strong) NSString                      *email;
@property (nonatomic, strong) NSString                      *pwd;

-(IBAction)buttonLinkPressed:(id)sender;
-(void) markFirstResponder:(int) row;
-(void) resignFirstResponder:(int) row;
@end
