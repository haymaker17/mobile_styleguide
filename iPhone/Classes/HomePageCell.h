//
//  HomePageCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface HomePageCell : UITableViewCell {
	
	UIImageView			*iv;
	UILabel				*lblHeading, *lblSubheading;

}

@property (strong, nonatomic) IBOutlet UIImageView			*iv;
@property (strong, nonatomic) IBOutlet UILabel				*lblHeading;
@property (strong, nonatomic) IBOutlet UILabel				*lblSubheading;

@end
