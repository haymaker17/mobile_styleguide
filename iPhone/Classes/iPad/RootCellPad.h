//
//  RootCellPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface RootCellPad : UITableViewCell {
	
	UILabel		*lblLabel;
	UIImageView	*iv;

}

@property (nonatomic, strong) IBOutlet UILabel		*lblLabel;
@property (nonatomic, strong) IBOutlet UIImageView	*iv;

@end
