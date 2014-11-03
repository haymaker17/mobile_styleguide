//
//  CXWaitViewController.h
//  FusionLab
//
//  Created by Richard Puckett on 4/17/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXDistractor.h"

@interface CXWaitViewController : UIViewController

@property (weak, nonatomic) IBOutlet CXDistractor *distractor;
@property (weak, nonatomic) IBOutlet UILabel *caption;
@property (strong, nonatomic) NSString *captionText;

@end
