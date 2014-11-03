//
//  SegDetailRowCellPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SegDetailRowCellPad : UITableViewCell {
	UILabel						*lblLabel, *lblValue;
	UIButton					*btnAction;
	UIImageView					*iv;

}

@property (nonatomic, strong) IBOutlet UILabel						*lblLabel;
@property (nonatomic, strong) IBOutlet UILabel						*lblValue;
@property (nonatomic, strong) IBOutlet UIButton						*btnAction;
@property (nonatomic, strong) IBOutlet UIImageView					*iv;
@end
