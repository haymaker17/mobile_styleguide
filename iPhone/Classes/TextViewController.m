//
//  TextViewController.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 19/02/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TextViewController.h"

@interface TextViewController ()
@property (strong, nonatomic) IBOutlet UITextView *textView;

@end

@implementation TextViewController


- (instancetype)initWithTitle:(NSString *)title
{
    TextViewController *vc = [[UIStoryboard storyboardWithName:@"TravelPointsBookingFlow" bundle:nil] instantiateViewControllerWithIdentifier:@"TextViewController"];
    vc.navigationItem.title = title;
    return vc;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    self.textView.text = self.text;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)setText:(NSString *)text
{
    _text = text;
    self.textView.text = text;
}



@end
