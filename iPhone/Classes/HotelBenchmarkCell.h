//
//  HotelBenchmarkCell.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 23/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HotelBenchmarkCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *lblPrice;
@property (weak, nonatomic) IBOutlet UILabel *lblLocationName;
@property (weak, nonatomic) IBOutlet UILabel *lblPerNight;

@end
