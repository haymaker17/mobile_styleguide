//
//  TrainSearchingVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "FindMe.h"
#import "TrainBooking.h"
#import "AmtrakShop.h"
#import "TrainBookListingVC.h"

@interface TrainSearchingVC : MobileViewController {
	UILabel			*lblMsg;
	AmtrakShop		*shop;
}


@property (strong, nonatomic) IBOutlet UILabel			*lblMsg;
@property (strong, nonatomic) AmtrakShop		*shop;
-(void)fetchTrains:(id)sender;

@end
