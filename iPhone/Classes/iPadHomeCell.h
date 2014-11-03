//
//  iPadHomeCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface iPadHomeCell : UITableViewCell {

	UILabel			*lblText, *lblLoading;
	UIImageView		*iv;
	UIView			*loadingView;
}


@property (strong, nonatomic) IBOutlet UILabel			*lblText;
@property (strong, nonatomic) IBOutlet UILabel			*lblLoading;
@property (strong, nonatomic) IBOutlet UIImageView		*iv;
@property (strong, nonatomic) IBOutlet UIView			*loadingView;

@end
