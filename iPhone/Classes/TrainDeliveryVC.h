//
//  TrainDeliveryVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TrainDetailVC.h"

@interface TrainDeliveryVC : MobileViewController <UIPickerViewDelegate, UIPickerViewDataSource>{
}

@property (nonatomic,strong)  UIPickerView			*dPicker;
@property (nonatomic,strong)  NSMutableArray		*aDeliveryOptions;
@property (nonatomic,strong)  UILabel				*lblDeliveryOption;
@property (nonatomic,strong)  TrainDetailVC			*parentVC;

@property (nonatomic,strong) IBOutlet UIView					*viewLoading;
@property (nonatomic,strong) IBOutlet UIActivityIndicatorView	*activity;
@property (nonatomic,strong) IBOutlet UILabel					*lblLoading;
@property (nonatomic,strong) IBOutlet UIButton					*btnBackground;

-(void)setSelectedOption:(NSString *)option;
-(void)fetchDeliveryOptions:(id)sender;

@end
