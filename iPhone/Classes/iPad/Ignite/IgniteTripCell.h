//
//  IgniteTripCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteTripCell : UITableViewCell
{
    UILabel         *lblTripName, *lblTripDescription, *lblDates;
}

@property (strong, nonatomic) IBOutlet UILabel         *lblTripName;
@property (strong, nonatomic) IBOutlet UILabel         *lblTripDescription;
@property (strong, nonatomic) IBOutlet UILabel         *lblDates;

@end
