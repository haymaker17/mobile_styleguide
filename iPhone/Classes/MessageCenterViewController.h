//
//  MessageCenterViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MessageCenterManager.h"
#import "DFPBannerView.h"

@interface MessageCenterViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>
{
}

@property (weak, nonatomic) IBOutlet UITableView *table;
@property (weak, nonatomic) MessageCenterManager *messageCenterManager;
@property (strong, nonatomic) DFPBannerView *dfpBannerView;

@end
