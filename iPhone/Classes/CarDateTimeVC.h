//
//  CarDateTimeVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DateTimeVC.h"
#import "CarViewController.h"

@interface CarDateTimeVC : DateTimeVC
{
	CarViewController	*parentVC;
	BOOL				isPickupDate;
}

@property (nonatomic, retain) CarViewController	*parentVC;
@property (nonatomic) BOOL						isPickupDate;

@end
