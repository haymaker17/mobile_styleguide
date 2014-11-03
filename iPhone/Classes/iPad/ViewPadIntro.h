//
//  ViewPadIntro.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewPadIntro : UIView
{
    UIImageView *iv, *iv2;
}

@property (strong, nonatomic) IBOutlet UIImageView *iv;
@property (strong, nonatomic) IBOutlet UIImageView *iv2;

@end
