//
//  EvaVoiceSearchViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 4/10/14.
//  Copyright (c) 2014 2014 Concur. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>
#import <Eva/Eva.h>

#import "CXPulseMic.h"
#import "CXDistractor.h"

extern NSString *FUSION2014_ERROR_MESSAGE;
extern NSString *FUSION2014_RESET_MESSAGE;

@interface EvaVoiceSearchViewController : UIViewController
    <AVAudioPlayerDelegate, AVSpeechSynthesizerDelegate, EvaDelegate, UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UILabel *prompt;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIButton *closeButton;
@property (weak, nonatomic) IBOutlet CXPulseMic *micButton;
@property (assign) EvaSearchCategory category;
@property BOOL isTextSearchQuery;

@end
