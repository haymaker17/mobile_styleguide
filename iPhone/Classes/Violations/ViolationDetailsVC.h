//
//  ViolationDetailsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@interface ViolationDetailsVC : MobileViewController
{
    UITextView *txtView;
    NSString *violationText;
}

@property (strong, nonatomic) IBOutlet UITextView *txtView;
@property (strong, nonatomic) NSString *violationText;

-(void)closeMe:(id)sender;
@end
