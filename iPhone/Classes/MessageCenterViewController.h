//
//  MessageCenterViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MessageCenterManager.h"
#import "DFPBannerView.h"
#import "GADAppEventDelegate.h"
#import "GADBannerViewDelegate.h"

@interface MessageCenterViewController : UIViewController <UITableViewDataSource, UITableViewDelegate, GADAppEventDelegate, GADBannerViewDelegate>
{
}

@property (weak, nonatomic) IBOutlet UITableView *table;
@property (weak, nonatomic) MessageCenterManager *messageCenterManager;
@property (strong, nonatomic) DFPBannerView *dfpBannerView;

@end
