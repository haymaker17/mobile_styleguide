//
//  OfferCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/26/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface OfferCell : UITableViewCell {
    UILabel         *lblTitle;
    UIImageView     *ivIcon;
    UIActivityIndicatorView *activity;
}

@property (strong, nonatomic) IBOutlet UILabel         *lblTitle;
@property (strong, nonatomic) IBOutlet UIImageView     *ivIcon;
@property (strong, nonatomic) IBOutlet UIActivityIndicatorView *activity;

-(void)configureLabelFontForLabel:(UILabel*)lbl WithText:(NSString*)txt;
@end
