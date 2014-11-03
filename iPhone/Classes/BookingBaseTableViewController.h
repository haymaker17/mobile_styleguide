//
//  BookingBaseTableViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseViewController.h"

@interface BookingBaseTableViewController : BookingBaseViewController
{
	UITableView		*tblView;
}

@property (nonatomic, strong) IBOutlet UITableView	*tblView;

@end
