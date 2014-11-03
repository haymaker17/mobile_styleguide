//
//  TrainDateVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DateTimeVC.h"
#import "BookingCellData.h"


@interface TrainDateVC : DateTimeVC
{
	BookingCellData		*bcdDate;
	BookingCellData		*bcdTime;
	TrainBookVC			*parentVC;
}

@property (nonatomic, strong) BookingCellData	*bcdDate;
@property (nonatomic, strong) BookingCellData	*bcdTime;
@property (nonatomic, strong) TrainBookVC		*parentVC;

- (void)populateDate:(BookingCellData*)bcdDt time:(BookingCellData*)bcdTm;

@end
