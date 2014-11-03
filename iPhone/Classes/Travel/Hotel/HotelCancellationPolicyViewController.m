//
//  HotelCancellationPolicyViewController.m
//  ConcurMobile
//
//  Created by echo on 9/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelCancellationPolicyViewController.h"

@interface HotelCancellationPolicyViewController ()
@property (nonatomic, readonly, strong) NSString *cancellationPolicy;
@property (nonatomic, readwrite, strong) IBOutlet UITextView *cancellationPolicyTextView;
@end

@implementation HotelCancellationPolicyViewController

- (id)initWithCancellationPolicy:(NSString *)cancellationPolicy
{
    self = [super initWithNibName:@"HotelCancellationPolicyViewController" bundle:nil];
    if (self) {
        _cancellationPolicy = cancellationPolicy;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self.cancellationPolicyTextView setText:self.cancellationPolicy];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

@end
