//
//  DetailApprovalCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface DetailApprovalCell : UITableViewCell {
	UILabel				*lblLabel, *lblValue, *lblSep;

}

@property (strong, nonatomic) IBOutlet UILabel				*lblLabel;
@property (strong, nonatomic) IBOutlet UILabel				*lblValue;
@property (strong, nonatomic) IBOutlet UILabel				*lblSep;

@end
