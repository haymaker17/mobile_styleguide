//
//  CancellationDetailsVC
//  ConcurMobile
//
//  Created by Chris Butcher on 1/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"

@interface CancellationDetailsVC : MobileViewController
{
    UITextView *txtView;
    NSString *cancellationText;
}

@property (strong, nonatomic) IBOutlet UITextView *txtView;
@property (strong, nonatomic) NSString *cancellationText;

-(void)closeMe:(id)sender;
@end
