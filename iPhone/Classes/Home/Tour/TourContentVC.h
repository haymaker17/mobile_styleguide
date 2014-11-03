//
//  TourContentVC.h
//  ConcurMobile
//
//  Created by Shifan Wu on 5/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface TourContentVC : MobileViewController
{
    NSString *imageName;
}

@property (nonatomic, strong) IBOutlet UIImageView *tourImage;
@property (nonatomic, strong) IBOutlet UILabel *lblUpper;
@property (nonatomic, strong) IBOutlet UILabel *lblLower;

@property (nonatomic, strong) NSString  *imageName;

- (id)initWithImageName:(NSString*)imageName;
- (UIFont*) getFontForLabelWidth:(CGFloat)labelWidth labelHeight:(CGFloat)labelHeight minmumFontSize:(int)fontSize fontName:(NSString*)fontName stringValue:(NSString*)value desiredFontSize:(int)desiredFontSize;
@end
