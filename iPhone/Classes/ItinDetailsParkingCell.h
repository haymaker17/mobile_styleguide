//
//  ItinDetailsParkingCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ItinDetailsParkingCell : UITableViewCell {
	
	UILabel			*lblVendor, *lblAddress1, *lblAddress2, *lblPhone;
	UIImageView		*ivVendor, *ivMap, *ivPhone;

}

@property (strong, nonatomic) IBOutlet UILabel			*lblVendor;
@property (strong, nonatomic) IBOutlet UILabel			*lblAddress1;
@property (strong, nonatomic) IBOutlet UILabel			*lblAddress2;
@property (strong, nonatomic) IBOutlet UILabel			*lblPhone;
@property (strong, nonatomic) IBOutlet UIImageView		*ivVendor;
@property (strong, nonatomic) IBOutlet UIImageView		*ivMap;
@property (strong, nonatomic) IBOutlet UIImageView		*ivPhone;

@end
