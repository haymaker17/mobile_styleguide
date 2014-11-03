//
//  FilterCheckBox.m
//  testSeg
//
//  Created by Ray Chi on 9/17/14.
//  Copyright (c) 2014 ConcurTech. All rights reserved.
//

#define Width 69.0f
#define Height 49.0f

#import "FilterCheckBox.h"

@interface FilterCheckBox()

@property (nonatomic,strong) UIView *containerView;
@property (nonatomic,strong) UILabel *labelText;

@property (nonatomic,strong) UIImageView *starView;



-(void)commonInit;

-(CGRect)roundRect:(CGRect)frameOrBound;

-(void)handleTapTapGestureRecognizerEvent:(UITapGestureRecognizer *)recognizer;

@end

@implementation FilterCheckBox

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:[self roundRect:frame]];
    if (self) {
        [self commonInit];
        self.isClick = NO;
    }
    return self;
}


- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if(self){
        [self commonInit];
        self.isClick = NO;
    }
    return self;
}

/**
 *  First Load Initiate function.
 */
- (void)commonInit{
    
    self.isClick = NO;
    self.on = NO;
    
    self.backgroundColor = [UIColor clearColor];
    self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, 69.0f, 49.0f);
    self.layer.borderWidth = 1;
    self.layer.borderColor = [[UIColor colorWithRed:167/255.0 green:182/255.0 blue:191/255.0 alpha:0.5] CGColor];
    
    //    _containerView = [[UIView alloc] initWithFrame:CGRectMake(self.frame.origin.x, self.frame.origin.y, 68.0f, 48.0f)];
    _containerView = [[UIView alloc] initWithFrame:self.bounds];
    _containerView.backgroundColor = [UIColor clearColor];
    [self addSubview:_containerView];
    
    _labelText = [[UILabel alloc] initWithFrame:_containerView.frame];
    _labelText.backgroundColor = [UIColor clearColor];
    [_labelText setTextAlignment:NSTextAlignmentCenter];
    _labelText.textColor = [UIColor blackColor];
    _labelText.font = [UIFont systemFontOfSize:15.0];
    _labelText.text = @"ALL";
    
    [_containerView addSubview:_labelText];
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                 action:@selector(handleTapTapGestureRecognizerEvent:)];
    [self addGestureRecognizer:tapGesture];
    
}

/**
 *  Change image to 3 star, 4 star, 5 star
 *
 *  @param starNo number of stars on the image
 */
- (void) changeImageStars:(NSInteger)starNo
{
    if(self.starView == nil){
        self.type = 2;
    }
    
    switch (starNo) {
        case 3:{
            self.starView.image = [UIImage imageNamed:@"hotel_star_rating3"];
            self.starView = [[UIImageView alloc] initWithFrame:CGRectMake(self.containerView.frame.origin.x+8, _containerView.frame.origin.y+22, 53, 19)];
            self.labelText.text = @"3+";
            break;
        }
        case 4:{
            self.starView.image = [UIImage imageNamed:@"hotel_star_rating4"];
            self.starView.frame = CGRectMake(self.containerView.frame.origin.x+7, _containerView.frame.origin.y+26, 54, 13);
            self.labelText.text = @"4+";
            break;
        }
        case 5:{
            self.starView.image = [UIImage imageNamed:@"hotel_star_rating5"];
            self.starView.frame = CGRectMake(self.containerView.frame.origin.x+4, _containerView.frame.origin.y+27, 60, 12);
            self.labelText.text = @"5";
            break;
        }
        default:
            break;
    }
}

/**
 *  Rewrite layout subview function
 */
- (void)layoutSubviews
{
    [super layoutSubviews];
    
    _containerView.layer.masksToBounds = YES;
    
    if(self.isOn){
        self.containerView.backgroundColor = [UIColor colorWithRed:0 green:120/255.0 blue:200/255.0 alpha:1];
        self.labelText.textColor = [UIColor whiteColor];
        self.labelText.font = [UIFont boldSystemFontOfSize:(self.type==3) ? 15.5 : 17];
        
        if(self.starView.image !=nil)
            self.starView.image = [self.starView.image imageTintedWithColor:[UIColor whiteColor]];
    }
    else{
        self.containerView.backgroundColor = [UIColor clearColor];
        self.labelText.textColor = [UIColor blackColor];
        self.labelText.font = [UIFont systemFontOfSize:15];
        
        if(self.starView.image !=nil)
            self.starView.image = [self.starView.image imageTintedWithColor:[UIColor blackColor]];
    }
}

- (void)setType:(NSInteger)type
{
    
    if(_type!=type){
        _type = type;
    }
    switch (_type) {            // "All" with font size 17
        case 1:
            //
            // Do nothing
            break;
        case 2:{                // "3+" with 3 stars image
            self.starView = [[UIImageView alloc] initWithFrame:CGRectMake(self.containerView.frame.origin.x+8, _containerView.frame.origin.y+23, 53, 19)];
            self.starView.image = [UIImage imageNamed:@"hotel_star_rating3"];
            self.starView.backgroundColor = [UIColor clearColor];
            [self.containerView addSubview:self.starView];
            self.labelText.frame = CGRectMake(self.containerView.frame.origin.x, self.containerView.frame.origin.y, 68, 33);
            self.labelText.text = @"3+";
            
            break;
        }
        case 3:{                // "5 miles" with font size 15
            
            self.labelText.font = [UIFont systemFontOfSize:15.0];
            break;
        }
        default:
            break;
    }
    
}


-(void)setText:(NSString *)text
{
    if (_text != text) {
        _text = text;
        _labelText.text = text;
    }
}

- (CGRect)roundRect:(CGRect)frameOrBounds
{
    CGRect newRect = frameOrBounds;
    newRect.size.height = Width;
    newRect.size.height = Height;
    
    return newRect;
}

- (void)handleTapTapGestureRecognizerEvent:(UITapGestureRecognizer *)recognizer
{
    if (recognizer.state == UIGestureRecognizerStateEnded) {
        if(self.isOn==YES && self.isClick == YES)
            return;
        
        [self setOn:!self.isOn];
        self.isClick = YES;
        //        [self sendActionsForControlEvents:UIControlEventValueChanged];
    }
}

- (void)setOn:(BOOL)on
{
    if(self.on == on){
        return;
    }
    _on = on;
    
    if(self.isOn){
        [UIView animateWithDuration:.6 animations:^{
            self.containerView.backgroundColor = [UIColor colorWithRed:0 green:120/255.0 blue:200/255.0 alpha:1];
            self.labelText.textColor = [UIColor whiteColor];
            self.labelText.font = [UIFont boldSystemFontOfSize:(self.type==3) ? 15.5 : 17];
            if(self.starView!=nil){
                self.starView.image = [self.starView.image imageTintedWithColor:[UIColor whiteColor]];
            }
            
        }completion:^(BOOL finished){
            
        }
         ];
    }
    else{
        
        [UIView animateWithDuration:.6 animations:^{
            self.containerView.backgroundColor = [UIColor clearColor];
            self.labelText.textColor = [UIColor blackColor];
            self.labelText.font = [UIFont systemFontOfSize:15];
            
            if(self.starView!=nil){
                self.starView.image = [self.starView.image imageTintedWithColor:[UIColor blackColor]];
            }
            
        }completion:^(BOOL finished){
        }
         ];
        
        
    }
    
    [self sendActionsForControlEvents:UIControlEventValueChanged];
}

@end

#pragma mark --- UIImage change tint color

@implementation UIImage (FilterCheckBox)

- (UIImage *)imageTintedWithColor:(UIColor *)color {
	if (color) {
		UIGraphicsBeginImageContextWithOptions([self size], NO, 0.f);
		
		CGRect rect = CGRectZero;
		rect.size = [self size];
		
		[color set];
		UIRectFill(rect);
		
		[self drawInRect:rect blendMode:kCGBlendModeDestinationIn alpha:1.0];
		
		UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
		UIGraphicsEndImageContext();
		
		return image;
	}
	
	return self;
}
@end



