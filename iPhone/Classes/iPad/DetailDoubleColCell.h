//
//  DetailDoubleColCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface DetailDoubleColCell : UITableViewCell {
	UILabel			*lblCol1, *lblCol2;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200	
	UIPopoverController *pickerPopOver;
#endif
}
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
@property (nonatomic, strong) UIPopoverController *pickerPopOver;
#endif
@property (strong, nonatomic) IBOutlet UILabel			*lblCol1;
@property (strong, nonatomic) IBOutlet UILabel			*lblCol2;

@end
