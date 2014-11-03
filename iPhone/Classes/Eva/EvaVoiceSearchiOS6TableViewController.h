//
//  EvaVoiceSearchiOS6TableViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/8/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Eva/Eva.h>
#import "DataConstants.h"
@import AVFoundation;
 
@interface EvaVoiceSearchiOS6TableViewController : UITableViewController <EvaDelegate, AVSpeechSynthesizerDelegate>


@property(nonatomic,retain) Eva *evaModule;
// Cancel button in navbar
@property (strong,nonatomic) IBOutlet UIBarButtonItem *cancel;
@property EvaSearchCategory inputSearchCategory;
// Starts the voice record.
- (IBAction)btnStartRecord:(id)sender;

@end
