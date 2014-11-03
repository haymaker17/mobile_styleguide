//
//  HotelBookingCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface HotelBookingCell : UITableViewCell {
    UILabel     *lblLabel, *lblValue;
    
    
}

@property (strong, nonatomic) IBOutlet UILabel     *lblLabel;
@property (strong, nonatomic) IBOutlet UILabel     *lblValue;
@property (strong, nonatomic) IBOutlet UILabel     *lblSubValue;

@end
